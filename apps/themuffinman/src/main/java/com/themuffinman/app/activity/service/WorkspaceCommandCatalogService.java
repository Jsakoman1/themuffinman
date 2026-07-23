package com.themuffinman.app.activity.service;
import com.themuffinman.app.activity.dto.*; import com.themuffinman.app.identity.model.AppUser; import com.themuffinman.app.identity.service.PersonalShortcutService; import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class WorkspaceCommandCatalogService {
 private static final String NAVIGATE = "NAVIGATE"; private static final String CREATE_ROUTE = "CREATE_ROUTE"; private static final String VISION_ROUTE = "VISION_ROUTE";
 private final PersonalShortcutService personalShortcutService;
 public WorkspaceCommandCatalogResponseDTO getCatalog(AppUser user) {
  var personal=personalShortcutService.getMine(user).stream().map(s->item("personal-"+s.targetType()+"-"+s.targetId(),"Personal",s.title(),"Pinned, permitted object",s.route(),NAVIGATE)).toList();
  return WorkspaceCommandCatalogResponseDTO.builder().personal(personal)
   .navigation(List.of(item("navigate-home","Navigate","Home","Your workspace overview","/home",NAVIGATE),item("navigate-activity","Navigate","Activity","Viewer-scoped updates and resumes","/activity",NAVIGATE),item("navigate-notifications","Navigate","Notifications","Attention and news","/notifications",NAVIGATE),item("navigate-work","Navigate","Find work","Browse permitted work","/work/find",NAVIGATE),item("navigate-chat","Navigate","Chat","Open conversations","/chat",NAVIGATE)))
   .create(List.of(item("create-quest","Create","Create a quest","Open the existing quest form","/work/quests/new",CREATE_ROUTE),item("create-thing","Create","List a thing","Open the existing thing listing flow","/things",CREATE_ROUTE),item("create-circle","Create","Create a circle","Open the existing circle flow","/circles",CREATE_ROUTE),item("create-chat","Create","Start a chat","Open the existing chat flow","/chat",CREATE_ROUTE)))
   .vision(List.of(item("vision-help","Vision","Ask Vision","Open consent-bound guided help","/vision",VISION_ROUTE))).build(); }
 private WorkspaceCommandItemDTO item(String id,String group,String label,String description,String route,String kind){return WorkspaceCommandItemDTO.builder().id(id).group(group).label(label).description(description).route(route).kind(kind).build();}
}
